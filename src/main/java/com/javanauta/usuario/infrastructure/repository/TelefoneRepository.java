package com.javanauta.usuario.infrastructure.repository;

import com.javanauta.usuario.infrastructure.entity.Telefone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

//Uma interface Repository para cada Entity
//O que colocar dentro do "< >" = Nome do entity, Tipo do ID
//Por que a Interface herda JpaRepository ? Para que não seja obrigatório o uso dos métodos, utilizamos quando for necessário
//Metodos save(), delete(), findAll(), findById()

@Repository
public interface TelefoneRepository extends JpaRepository<Telefone, Long> {

    @Modifying
    @Transactional
    int deleteByIdAndUsuarioId(Long id, Long usuarioId);
}
