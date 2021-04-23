package com.eldersoss.identitykit;

import com.eldersoss.identitykit.storage.TokenStorage;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by IvanVatov on 8/28/2017.
 */

public class TestTokenStorage implements TokenStorage {
    private String token;

    @Nullable
    @Override
    public String read(@NotNull String key) { return token; }

    @Override
    public void delete(@NotNull String key) {
        token = null;
    }

    @Override
    public void write(@NotNull String key, @NotNull String token) {
        this.token = token;
    }
}
