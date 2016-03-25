package ru.ssp.synch.impl.config;

/**
 * Created by PakAI on 23.03.2016.
 */
public abstract class JiraConfiguration {

    private String url;

    private String user;

    private String password;

    /**
     * Gets user.
     *
     * @return Value of user.
     */
    public String getUser() {
        return user;
    }

    /**
     * Sets new url.
     *
     * @param url New value of url.
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Sets new password.
     *
     * @param password New value of password.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gets password.
     *
     * @return Value of password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Gets url.
     *
     * @return Value of url.
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets new user.
     *
     * @param user New value of user.
     */
    public void setUser(String user) {
        this.user = user;
    }
}
