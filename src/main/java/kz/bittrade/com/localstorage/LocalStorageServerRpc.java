package kz.bittrade.com.localstorage;


import com.vaadin.shared.communication.ServerRpc;

public interface LocalStorageServerRpc extends ServerRpc {

    public void reportValue(String key, String value);
}