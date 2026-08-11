package com.javanauta.usuario.business;


import com.javanauta.usuario.business.converter.UsuarioConverter;
import com.javanauta.usuario.business.dto.EnderecoDTO;
import com.javanauta.usuario.business.dto.TelefoneDTO;
import com.javanauta.usuario.business.dto.UsuarioRequestDTO;
import com.javanauta.usuario.business.dto.UsuarioResponseDTO;
import com.javanauta.usuario.infrastructure.entity.Endereco;
import com.javanauta.usuario.infrastructure.entity.Telefone;
import com.javanauta.usuario.infrastructure.entity.Usuario;
import com.javanauta.usuario.infrastructure.exceptions.ConflictException;
import com.javanauta.usuario.infrastructure.exceptions.IllegalArgumentException;
import com.javanauta.usuario.infrastructure.exceptions.ResourceNotFound;
import com.javanauta.usuario.infrastructure.exceptions.UnauthorizedException;
import com.javanauta.usuario.infrastructure.repository.EnderecoRepository;
import com.javanauta.usuario.infrastructure.repository.TelefoneRepository;
import com.javanauta.usuario.infrastructure.repository.UsuarioRepository;
import com.javanauta.usuario.infrastructure.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final EnderecoRepository enderecoRepository;
    private final TelefoneRepository telefoneRepository;
    private final UsuarioConverter usuarioConverter;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public UsuarioResponseDTO salvarUsuario (UsuarioRequestDTO usuarioRequestDTO) {   // Recebi um UsuarioDTO
        existsByEmail(usuarioRequestDTO.getEmail());

        usuarioRequestDTO.setSenha(passwordEncoder.encode(usuarioRequestDTO.getSenha()));

        Usuario usuario = usuarioConverter.paraUsuario(usuarioRequestDTO); //Converti UsuarioDTO para UsuarioEntity
        usuario = usuarioRepository.save(usuario);  //Salvei no banco de dados o UsuarioEntity
        return usuarioConverter.paraUsuarioResponseDTO(usuario);    //Converti UsuarioEntity para UsuarioDTO e retornei
    }

    public String autenticadorJWT (UsuarioRequestDTO usuarioRequestDTO) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(usuarioRequestDTO.getEmail(),
                            usuarioRequestDTO.getSenha())
            );
            return geradorTokenJWT(authentication);
        } catch (BadCredentialsException | UsernameNotFoundException | AuthorizationDeniedException e) {
            throw new UnauthorizedException(e.getMessage());
        }
    }

    private String geradorTokenJWT(Authentication authentication) {
        return "Bearer " + jwtUtil.generateToken(authentication.getName());
    }

    public void existsByEmail(String email) {
        if(!email.contains("@") || !email.contains(".")) {
            throw new IllegalArgumentException("Email incompleto, acrescente '@' ou '.' " + email );
        }

        boolean existe = usuarioRepository.existsByEmail(email);
        if (existe) {
            throw new ConflictException("Email já cadastrado " + email);
        }
    }

    public UsuarioResponseDTO retornarEmail(String email){
        try {

            return usuarioConverter.paraUsuarioResponseDTO
                (usuarioRepository.findByEmail(email).orElseThrow(
                ()-> new ConflictException("Email não encontrado: " + email)));

        } catch (Exception e) {
            throw new ConflictException("Email não encontrado " + email);
        }
    }

    public void deletarPorEmail(String email) {
        usuarioRepository.deleteByEmail(email);
    }

    public UsuarioResponseDTO atualizarUsuario (String token, UsuarioRequestDTO usuarioRequestDTO) {
        //Buscar o email através do token (tirar a obrigatoriedade do email)
        String email = extracaoEmail(token);
        //Se tiver uma nova senha, fazer encriptografia
        usuarioRequestDTO.setSenha(usuarioRequestDTO.getSenha() != null
                ? passwordEncoder.encode(usuarioRequestDTO.getSenha()) : null);

        //Buscar os dados do usuário no banco de dados
        Usuario usuarioEntity = usuarioRepository.findByEmail(email).orElseThrow(
                ()-> new RuntimeException("Email não encontrado " + usuarioRequestDTO.getEmail()) );
        //usuarioEntity.setEmail(usuarioDTO.getEmail());
        //Mesclou os dados que recebeu na requisição DTO e atualizou o que foi passado no banco de dados
        Usuario usuario = usuarioConverter.atualizarUsuario(usuarioEntity , usuarioRequestDTO);
        //Salvou o que foi convertido e depois converteu usuario para usuarioDTO, tendo o retorno.
        usuarioRepository.save(usuario);
            return usuarioConverter.paraUsuarioResponseDTO(usuario);
    }

    public EnderecoDTO atualizaEndereco(Long idEndereco, EnderecoDTO enderecoDTO) {

        Endereco enderecoEntity = enderecoRepository.findById(idEndereco).orElseThrow(
                ()-> new ConflictException("ID do endereço não encontrado " + idEndereco));
        Endereco endereco = usuarioConverter.atualizarEndereco(enderecoEntity ,enderecoDTO);
        enderecoRepository.save(endereco);
            return usuarioConverter.paraEnderecoDTO(endereco);
    }

    public TelefoneDTO atualizaTelefone(Long idTelefone, TelefoneDTO telefoneDTO) {

        Telefone telefoneEntity = telefoneRepository.findById(idTelefone).orElseThrow(
                ()-> new ConflictException("ID do telefone não encontrado " + idTelefone));
        Telefone telefone = usuarioConverter.atualizarTelefone(telefoneEntity, telefoneDTO);
        telefoneRepository.save(telefone);
            return usuarioConverter.paraTelefoneDTO(telefone);
    }

    public EnderecoDTO cadastroEnderecoDTO (String token ,EnderecoDTO enderecoDTO) {
        String email = extracaoEmail(token);

        Usuario usuarioEntity = usuarioRepository.findByEmail(email).orElseThrow(
                ()-> new ConflictException("Email não encontrado " + email)
        );
        Endereco endereco = usuarioConverter.paraEnderecoEntity(enderecoDTO, usuarioEntity.getId());

        Endereco enderecoEntity = enderecoRepository.save(endereco);

            return usuarioConverter.paraEnderecoDTO(enderecoEntity);

    }

    public TelefoneDTO cadastroTelefoneDTO (String token ,TelefoneDTO telefoneDTO) {
        String email = extracaoEmail(token);

        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow(
                ()-> new ConflictException("Email não encontrado " + email)
        );

        Telefone telefoneEntity = usuarioConverter.paraTelefoneEntity(telefoneDTO, usuario.getId());

        telefoneRepository.save(telefoneEntity);

            return usuarioConverter.paraTelefoneDTO(telefoneEntity);
    }

    private String extracaoEmail (String token) {
        return jwtUtil.extrairEmailToken(token.substring(7));
    }


    public void deletarTelefone (Long id, String token) {

        String email = extracaoEmail(token);

        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow(
                ()-> new ResourceNotFound("Email não encontrado " + email)
        );


        int linhasAfetadas = telefoneRepository.deleteByIdAndUsuarioId(id, usuario.getId());
        if (linhasAfetadas == 0) {
            throw new ResourceNotFound("Telefone não encontrado ou não pertence ao usuário");
        }

    }

    public void deletarEndereco (Long id, String token) {

        String email = extracaoEmail(token);

        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow(
                ()-> new ResourceNotFound("Email não encontrado " + email)
        );


        int linhasAfetadas = enderecoRepository.deleteByIdAndUsuarioId(id, usuario.getId());
        if (linhasAfetadas == 0) {
            throw new ResourceNotFound("Endereço não encontrado ou não pertence ao usuário");
        }



    }
}
