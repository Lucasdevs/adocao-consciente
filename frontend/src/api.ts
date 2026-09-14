export class ApiError extends Error {
  constructor(message: string, public status: number) { super(message); }
}

export async function api<T>(path: string, method = 'GET', body?: unknown): Promise<T> {
  const headers: Record<string, string> = { 'Content-Type': 'application/json' };
  if (method !== 'GET') {
    const token = await api<{ token: string; headerName: string }>('/auth/csrf');
    headers[token.headerName] = token.token;
  }
  let response: Response;
  try {
    response = await fetch('/api' + path, { method, headers, credentials: 'same-origin', body: body === undefined ? undefined : JSON.stringify(body) });
  } catch {
    throw new ApiError('Não foi possível conectar. Verifique sua conexão e tente novamente.', 0);
  }
  if (!response.ok) {
    const error = await response.json().catch(() => ({}));
    throw new ApiError(error.message || 'Não foi possível concluir a ação. Tente novamente.', response.status);
  }
  return response.status === 204 ? undefined as T : response.json();
}
