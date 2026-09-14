export type Role = 'ADOTANTE' | 'RESPONSAVEL';
export type RequestStatus = 'PENDENTE' | 'APROVADA' | 'RECUSADA' | 'CANCELADA' | 'CONCLUIDA';
export interface User { id: number; name: string; email: string; role: Role; }
export interface AnimalInput {
  name: string; species: 'CAO' | 'GATO'; size: 'PEQUENO' | 'MEDIO' | 'GRANDE'; ageMonths: number;
  city: string; description: string; care: string; photoUrl: string; vaccinated: boolean; neutered: boolean;
}
export interface Animal extends AnimalInput {
  id: number; status: 'DISPONIVEL' | 'EM_PROCESSO' | 'ADOTADO'; ownerId: number; ownerName: string;
}
export interface AdoptionRequest {
  id: number; animal: Animal; adopter: User; housing: string; routine: string; experience: string;
  status: RequestStatus; decisionNote: string; createdAt: string;
}
