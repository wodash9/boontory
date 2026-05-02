import Keycloak from 'keycloak-js'
import type { AuthConfig } from './authConfig'

export type KeycloakLike = Pick<
  Keycloak,
  'authenticated' | 'token' | 'tokenParsed' | 'init' | 'updateToken' | 'login' | 'logout' | 'createLogoutUrl'
>

export function createKeycloakClient(config: AuthConfig): KeycloakLike {
  return new Keycloak({
    url: config.url,
    realm: config.realm,
    clientId: config.clientId,
  })
}
