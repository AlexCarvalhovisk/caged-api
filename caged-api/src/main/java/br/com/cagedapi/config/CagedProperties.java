package br.com.cagedapi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "caged")
public record CagedProperties(
        String ftpHost,
        String ftpDir,
        String pastaDados,
        int ufRs,
        int codViamao,
        String charset,
        int ftpTimeoutSegundos) {
}
