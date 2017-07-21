package com.netply.zero.service.base.permissions;

public interface PermissionsCallback {
    void permissionGranted(String permission);

    void permissionDenied(String permission);
}
