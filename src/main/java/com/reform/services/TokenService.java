package com.reform.services;

import com.reform.entities.*;
import com.reform.repositories.TokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenService {

    private final TokenRepository tokenRepository;

    // 🔹 Generate Tokens When a New Subscription is Purchased
    public void generateTokensForClient(Client client, Product product) {
        //tokenQty - 1 as we generate the token after the first session
        List<Token> tokens = IntStream.range(0, product.getTokenQty())
                .mapToObj(i -> Token.TokenBuilder.aToken()
                        .withClient(client)
                        .withProduct(product)
                        .withTokenValue(product.getPrice() / product.getTokenQty())
                        .withExpiresAt(product.getAvailableUntil())
                        .withStatus(TokenStatus.AVAILABLE)
                        .build()
                )
                .toList();

        client.setTokens(tokens);
        tokenRepository.saveAll(tokens);
    }

    // 🔹 Deduct a Token When a Session is Held
    public Token consumeToken(Client client, LocalDate sessionDate) {
        List<Token> clientAvailableTokens = tokenRepository.findTokensByClientAndStatus(client, TokenStatus.AVAILABLE);

        if (clientAvailableTokens.isEmpty()) {
            log.error("🚨🚨🚨 BIG RED WARNING: Client {} has NO valid tokens! Extending all tokens by 7 days!", client.getName());
            extendTokens(client);
        }

        Token tokenUsed = clientAvailableTokens.stream().findFirst().get();
        tokenUsed.setUsedAt(sessionDate);
        tokenUsed.setStatus(TokenStatus.USED);

        log.info("✅ Token consumed for client {} on {}", client.getName(), sessionDate);
        return tokenUsed;
    }

    private void extendTokens(Client client) {
        List<Token> tokens = tokenRepository.findByClient(client);
        tokens.forEach(token -> token.setExpiresAt(token.getExpiresAt().plusDays(7)));
        tokenRepository.saveAll(tokens);
        log.info("📅 Extended all tokens for {} by 7 days.", client.getName());
    }


    // 🔹 Check if Token is Still Valid
    private boolean isValidForUse(Token token, LocalDate sessionDate) {
        return (token.getProduct().getAvailableFrom() == null || !sessionDate.isAfter(token.getProduct().getAvailableUntil()));
    }

    /**
     * Checks if a client has at least one available token.
     */
    public boolean hasAvailableTokens(Client client) {
        return tokenRepository.countAvailableTokens(client) > 0;
    }

    public void warnExpiringTokens() {
        LocalDate now = LocalDate.now();
        List<Token> expiringTokens = tokenRepository.findExpiringTokens(now.plusDays(7));

        expiringTokens.forEach(token ->
                log.warn("⚠️ Client {} has unused tokens expiring soon on {}!",
                        token.getClient().getName(), token.getExpiresAt()));
    }

}
