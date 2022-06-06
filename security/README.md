# Nestwave's Device to Cloud Bridge Sensitive Data

This folder is a placeholder for sensitive data files that are required for proper working of Nestwave's DCB.

## Sensitive Data

The file `sensitive-config.ini` defines environment variables used in `application.yml`. An example of such file is:
```ini
JDBC_USERNAME=username
JDBC_PASSWORD=aSecurePassword
KEY_STORE_PASSWORD=anOtherSecurePassword
```

The following variables are supported:
- JDBC_USERNAME: login for accessing the tracking database used to store navigation results.
- JDBC_PASSWORD: password for accessing the tracking database.
- KEY_STORE_PASSWORD: password to decrypt SSL certifcate from keystore.
- TRAXMATE_TOKEN: Token to access Traxmate database. If omitted, then the Traxmate plugin is disabled. Traxmate is an example of a plugin done with an external partner and is not mandatory.  

**NB:** Files, in this folder shall never be committed to GIT repository, or should be encrypted using `git-crypt`.
