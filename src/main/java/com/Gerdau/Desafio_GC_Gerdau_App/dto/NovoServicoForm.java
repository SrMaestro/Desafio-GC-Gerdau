package com.Gerdau.Desafio_GC_Gerdau_App.dto;

import com.Gerdau.Desafio_GC_Gerdau_App.model.Condicao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NovoServicoForm {

    @NotBlank
    @Size(max = 50)
    private String areaServico;

    @NotBlank
    @Size(max = 255)
    private String textoBreve;

    @NotBlank
    @Size(max = 10)
    @Pattern(regexp = "[A-Za-z0-9]*", message = "use apenas letras e números")
    private String abreviatura;

    @Size(max = 15)
    @Pattern(regexp = "(_[A-Za-z0-9]*)?", message = "deve começar com _ (ex.: _CONCRE)")
    private String complemento;

    @NotNull
    private Condicao condicao;

    @NotBlank
    @Pattern(regexp = "\\d{7}", message = "CNAE deve ter 7 dígitos")
    private String cnae;

    @Size(max = 100)
    private String criadorCodigo;
}
