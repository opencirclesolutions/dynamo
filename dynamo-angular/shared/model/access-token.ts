/**
Access token supplied by Keycloak SSO
**/
export interface AccessToken {
  realm_access: {
    roles: string[];
  }
}
