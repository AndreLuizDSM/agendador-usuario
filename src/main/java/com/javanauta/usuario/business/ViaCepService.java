package com.javanauta.usuario.business;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javanauta.usuario.business.dto.ViaCepDTO;
import com.javanauta.usuario.infrastructure.clients.ViaCepClient;
import com.javanauta.usuario.infrastructure.exceptions.IllegalArgumentException;
import com.javanauta.usuario.infrastructure.exceptions.ResourceNotFound;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ViaCepService {


    private final ViaCepClient cepClient;
    private final ObjectMapper objectMapper;
    private final StringRedisTemplate redisTemplate;


    public ViaCepDTO buscaCep(String cep) {
        try {
            String cepLimpo = verificaCep(cep);
            String key = "enderecos" + cepLimpo;


            String dadosCacheRedis = redisTemplate.opsForValue().get(key);

            if (Objects.nonNull(dadosCacheRedis)) {

                return objectMapper.readValue(dadosCacheRedis, ViaCepDTO.class);
            }

            ViaCepDTO dto = cepClient.buscaDadosDeEndereco(cepLimpo);

            if (Objects.nonNull(dto) && Objects.nonNull(dto.cep())) {
                String stringDTO = objectMapper.writeValueAsString(dto);

                redisTemplate.opsForValue().set(key, stringDTO, Duration.ofDays(5));
            }

            return dto;
        } catch (Exception e) {

            throw new ResourceNotFound("Erro ao obter cep", e);
        }
    }

//    private void existeCep(ViaCepDTO cepDTO){
//        if (cepDTO.cep() == null){
//            throw new ResourceNotFound("Cep não existe");
//        }
//    }

    private String verificaCep(String cep) {
        String cepFormatado = cep.replace(" ", "")
                .replace("-","");

        if (!cepFormatado.matches("\\d+") || cepFormatado.length() != 8) {
            throw new IllegalArgumentException("Cep deve conter 8 digitos e apenas números " );
        }

        return cepFormatado;
    }
}

