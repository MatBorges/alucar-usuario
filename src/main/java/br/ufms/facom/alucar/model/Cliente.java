package br.ufms.facom.alucar.model;

import java.time.LocalDate;
import java.time.Period;
import java.util.Objects;


public class Cliente {

    private String cpf;
    private String nome;
    private String cnh;
    private LocalDate validadeCnh;
    private LocalDate dataNascimento;
    private String telefone;
    private String email;
    private String endereco;


    private boolean ativo = true;

    public Cliente() {
    }

    public Cliente(String cpf, String nome, String cnh, LocalDate validadeCnh,
                   LocalDate dataNascimento, String telefone, String email,
                   String endereco) {
        this.cpf = cpf;
        this.nome = nome;
        this.cnh = cnh;
        this.validadeCnh = validadeCnh;
        this.dataNascimento = dataNascimento;
        this.telefone = telefone;
        this.email = email;
        this.endereco = endereco;
    }


    public boolean possuiCnhValida() {
        return validadeCnh != null && !validadeCnh.isBefore(LocalDate.now());
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public int getIdade() {
        if (dataNascimento == null) {
            return 0;
        }
        return Period.between(dataNascimento, LocalDate.now()).getYears();
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCnh() {
        return cnh;
    }

    public void setCnh(String cnh) {
        this.cnh = cnh;
    }

    public LocalDate getValidadeCnh() {
        return validadeCnh;
    }

    public void setValidadeCnh(LocalDate validadeCnh) {
        this.validadeCnh = validadeCnh;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Cliente outro)) {
            return false;
        }
        return Objects.equals(cpf, outro.cpf);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cpf);
    }

    @Override
    public String toString() {
        return cpf + " - " + nome;
    }
}
