package com.eldersoss.identitykit;

import com.eldersoss.identitykit.storage.TokenStorage;

import org.jetbrains.annotations.Nullable;

/**
 * Created by IvanVatov on 8/28/2017.
 */

public class TestTokenStorage implements TokenStorage {
    private String token;

    @Nullable
    @Override
    public String read(String key) {
        return token;
    }

    @Override
    public void delete(String key) {
        token = null;
    }

    @Override
    public void write(String key,String token) {
        this.token = token;
    }
}
