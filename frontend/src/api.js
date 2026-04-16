const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

export async function apiFetch(path, options = {}) {
  const headers = new Headers(options.headers || {});

  if (!headers.has("Content-Type") && options.body && !(options.body instanceof FormData)) {
    headers.set("Content-Type", "application/json");
  }

  return fetch(`${API_BASE_URL}${path}`, {
    credentials: "include",
    ...options,
    headers,
  });
}

export async function readApiError(response, fallbackMessage) {
  try {
    const data = await response.json();
    return {
      message: data.message || fallbackMessage,
      fieldErrors: data.fieldErrors || {},
    };
  } catch {
    return {
      message: fallbackMessage,
      fieldErrors: {},
    };
  }
}

export { API_BASE_URL };