package com.eldersoss.identitykit;

import com.eldersoss.identitykit.oauth2.Token;
import com.eldersoss.identitykit.storage.TokenStorage;

import org.jetbrains.annotations.Nullable;

/**
 * Created by IvanVatov on 8/28/2017.
 */

public class TestTokenStorage implements TokenStorage {
    private Token token;

    @Nullable
    @Override
    public Token readToken() {
        return token;
    }

    @Override
    public void deleteToken() {
        token = null;
    }

    @Override
    public void writeToken(Token token) {
        this.token = token;
    }
}
