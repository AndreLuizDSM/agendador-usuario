package com.javanauta.usuario.business;


import com.javanauta.usuario.business.dto.ViaCepDTO;
import com.javanauta.usuario.infrastructure.clients.ViaCepClient;
import com.javanauta.usuario.infrastructure.exceptions.IllegalArgumentException;
import com.javanauta.usuario.infrastructure.exceptions.ResourceNotFound;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class ViaCepService {


    private final ViaCepClient cepClient;
    private final ObjectMapper objectMapper;
    private final StringRedisTemplate redisTemplate;


    public ViaCepDTO buscaCep(String cep) {
        try {
            String cepLimpo = verificaCep(cep);
            String key = "enderecos" + cepLimpo;


            log.debug("iniciando busca pelo redis {}" , cep);
            String dadosCacheRedis = redisTemplate.opsForValue().get(key);
            log.debug("Finalizando busca pelo redis {}" , cep);

            if (Objects.nonNull(dadosCacheRedis)) {

                return objectMapper.readValue(dadosCacheRedis, ViaCepDTO.class);
            }

            log.debug("iniciando busca pelo client via cep: {}" , cep);
            ViaCepDTO dto = cepClient.buscaDadosDeEndereco(cepLimpo);
            log.debug("Finalizando busca pelo client via cep: {}" , cep);

            if (Objects.nonNull(dto) && Objects.nonNull(dto.cep())) {
                String stringDTO = objectMapper.writeValueAsString(dto);

                redisTemplate.opsForValue().set(key, stringDTO, Duration.ofDays(5));
            }

            return dto;
        } catch (Exception e) {

            log.error("Error ao buscar cep: {}" , cep);
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

