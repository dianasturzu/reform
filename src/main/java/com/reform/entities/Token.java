package com.reform.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Token {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @OneToOne
    @JoinColumn(name = "session_id")
    private Session session;

    private Double tokenValue;

    private LocalDate expiresAt; // Expiration date

    private LocalDate usedAt;    // Date the token was used

    private TokenStatus status;   // AVAILABLE / USED

    public static final class TokenBuilder {
        private final Token token;

        private TokenBuilder() {
            token = new Token();
        }

        public static TokenBuilder aToken() {
            return new TokenBuilder();
        }

        public TokenBuilder withId(Long id) {
            token.setId(id);
            return this;
        }

        public TokenBuilder withProduct(Product product) {
            token.setProduct(product);
            return this;
        }

        public TokenBuilder withClient(Client client) {
            token.setClient(client);
            return this;
        }

        public TokenBuilder withSession(Session session) {
            token.setSession(session);
            return this;
        }

        public TokenBuilder withTokenValue(Double tokenValue) {
            token.setTokenValue(tokenValue);
            return this;
        }

        public TokenBuilder withExpiresAt(LocalDate expiresAt) {
            token.setExpiresAt(expiresAt);
            return this;
        }

        public TokenBuilder withUsedAt(LocalDate usedAt) {
            token.setUsedAt(usedAt);
            return this;
        }

        public TokenBuilder withStatus(TokenStatus status) {
            token.setStatus(status);
            return this;
        }

        public Token build() {
            return token;
        }
    }
}
