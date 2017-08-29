package com.eldersoss.identitykit;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;

/**
 * Created by IvanVatov on 8/28/2017.
 */

public class TestCredentialsProvider implements CredentialsProvider {
    @Override
    public void provideCredentials(Function2<? super String, ? super String, Unit> handler) {
        handler.invoke("gg@eldersoss.com", "ggPass123");
    }
}
