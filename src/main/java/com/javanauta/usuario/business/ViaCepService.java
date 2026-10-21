package com.javanauta.usuario.business;

import com.javanauta.usuario.business.dto.ViaCepDTO;
import com.javanauta.usuario.infrastructure.clients.ViaCepClient;
import com.javanauta.usuario.infrastructure.exceptions.IllegalArgumentException;
import com.javanauta.usuario.infrastructure.exceptions.ResourceNotFound;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ViaCepService {

    @Autowired
    private ViaCepClient cepClient;

    public ViaCepDTO buscaCep(String cep) {
        ViaCepDTO cepValido = cepClient.buscaDadosDeEndereco(verificaCep(cep));
        existeCep(cepValido);
        return cepValido;
    }

    private void existeCep(ViaCepDTO cepDTO){
        if (cepDTO.cep() == null){
            throw new ResourceNotFound("Cep não existe");
        }
    }

    private String verificaCep(String cep) {
        String cepFormatado = cep.replace(" ", "")
                .replace("-","");

        if (!cepFormatado.matches("\\d+") || cepFormatado.length() != 8) {
            throw new IllegalArgumentException("Cep deve conter 8 digitos e apenas números " );
        }

        return cepFormatado;
    }
}
