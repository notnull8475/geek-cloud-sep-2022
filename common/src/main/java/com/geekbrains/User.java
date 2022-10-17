package com.geekbrains;

import java.util.UUID;

public record User(
        String name,
        UUID uuid,
        int id,
        String path) {


}
