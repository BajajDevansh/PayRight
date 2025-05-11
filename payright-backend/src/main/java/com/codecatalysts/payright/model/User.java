package com.codecatalysts.payright.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Set; // For roles

@Data

@Document(collection = "users")
public class User {
    public User(){

    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    @Id
    private String id;
    @Indexed(unique = true)
    private String username;
    private String password;
    private String fullName;

    public String getWalletId() {
        return walletId;
    }

    public void setWalletId(String walletId) {
        this.walletId = walletId;
    }

    private String walletId;

    public User(String username, String password, String fullName,String walletId) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.walletId=walletId;
    }
}

