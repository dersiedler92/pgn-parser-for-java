import axios from "axios";
import { AuthApi, Configuration, DefaultApi } from "./index";

/**
 * Shared axios instance and API clients. `withCredentials: true` is required so the browser
 * carries the `JSESSIONID` cookie produced by the Lichess OAuth flow on the backend.
 *
 * Basic Auth (alpa/secret123) is preserved here for the non-OAuth endpoints; the Lichess
 * OAuth session is layered on top via the cookie.
 */
const configuration = new Configuration({
  basePath: "/api",
  username: "alpa",
  password: "secret123",
});

export const axiosInstance = axios.create({ withCredentials: true });

export const api = new DefaultApi(configuration, undefined, axiosInstance);
export const authApi = new AuthApi(configuration, undefined, axiosInstance);
