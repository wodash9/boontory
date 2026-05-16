import Keycloak from 'keycloak-js'

export type KeycloakClientConfig = {
  url: string
  realm: string
  clientId: string
}

export type KeycloakLike = Pick<
  Keycloak,
  'authenticated' | 'token' | 'tokenParsed' | 'init' | 'updateToken' | 'login' | 'register' | 'logout' | 'createLogoutUrl'
>

export function createKeycloakClient(config: KeycloakClientConfig): KeycloakLike {
  return new Keycloak({
    url: config.url,
    realm: config.realm,
    clientId: config.clientId,
  })
}
