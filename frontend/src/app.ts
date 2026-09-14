import { Component, ElementRef, OnInit, ViewChild, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { api, ApiError } from './api';
import { Animal, AnimalInput, AdoptionRequest, RequestStatus, Role, User } from './models';

type View = 'catalogo' | 'orientacoes' | 'animais' | 'pedidos';
type Modal = 'detail' | 'auth' | 'animal' | 'apply' | 'decision' | null;

@Component({ selector: 'app-root', standalone: true, imports: [CommonModule, FormsModule], templateUrl: './app.html' })
export class App implements OnInit {
  @ViewChild('dialog', { static: true }) dialog!: ElementRef<HTMLDialogElement>;
  user = signal<User | null>(null);
  view = signal<View>('catalogo');
  animals = signal<Animal[]>([]);
  requests = signal<AdoptionRequest[]>([]);
  selected = signal<Animal | null>(null);
  modal = signal<Modal>(null);
  busy = signal(false);
  loading = signal(true);
  error = signal('');
  notice = signal('');
  demo = signal(false);
  editingId: number | null = null;
  query = ''; species = ''; size = '';
  authMode: 'login' | 'register' = 'login';
  authForm = { name: '', email: '', password: '', role: 'ADOTANTE' as Role };
  animalForm: AnimalInput = this.blankAnimal();
  application = { housing: '', routine: '', experience: '', commitment: false };
  decisionRequest: AdoptionRequest | null = null;
  decisionStatus: RequestStatus = 'APROVADA';
  decisionNote = '';
  labels: Record<string, string> = {
    CAO: 'Cão', GATO: 'Gato', PEQUENO: 'Pequeno', MEDIO: 'Médio', GRANDE: 'Grande',
    DISPONIVEL: 'Disponível', EM_PROCESSO: 'Em processo', ADOTADO: 'Adotado', PENDENTE: 'Em análise',
    APROVADA: 'Aprovada', RECUSADA: 'Encerrada pelo responsável', CANCELADA: 'Cancelada', CONCLUIDA: 'Adoção concluída'
  };
  async ngOnInit() {
    const [me, info] = await Promise.allSettled([api<User>('/auth/me'), api<{ demo: boolean }>('/auth/info')]);
    if (me.status === 'fulfilled') this.user.set(me.value);
    if (info.status === 'fulfilled') this.demo.set(info.value.demo);
    await this.refresh();
  }
  blankAnimal(): AnimalInput {
    return { name: '', species: 'CAO', size: 'MEDIO', ageMonths: 12, city: '', description: '', care: '', photoUrl: '', vaccinated: false, neutered: false };
  }
  async navigate(view: View) {
    this.view.set(view); this.error.set(''); this.notice.set(''); await this.refresh();
  }
  async refresh() {
    this.loading.set(true);
    try {
      if (this.view() === 'catalogo') {
        const params = new URLSearchParams();
        if (this.query) params.set('q', this.query);
        if (this.species) params.set('species', this.species);
        if (this.size) params.set('size', this.size);
        this.animals.set(await api<Animal[]>('/animals?' + params));
      } else if (this.view() === 'animais') this.animals.set(await api<Animal[]>('/my/animals'));
      else if (this.view() === 'pedidos') this.requests.set(await api<AdoptionRequest[]>('/requests'));
    } catch (e) { this.fail(e); }
    finally { this.loading.set(false); }
  }
  fail(e: unknown) {
    this.error.set(e instanceof Error ? e.message : 'Ocorreu um erro. Tente novamente.');
    if (e instanceof ApiError && e.status === 401) this.user.set(null);
  }
  open(modal: Modal) {
    this.error.set(''); this.notice.set(''); this.modal.set(modal);
    if (!this.dialog.nativeElement.open) this.dialog.nativeElement.showModal();
  }
  close() { if (this.busy()) return; this.dialog.nativeElement.close(); this.modal.set(null); this.error.set(''); }
  cancelDialog(event: Event) { event.preventDefault(); this.close(); }
  showAnimal(animal: Animal) { this.selected.set(animal); this.open('detail'); }
  showAuth() { this.authForm.password = ''; this.open('auth'); }
  async authenticate() {
    this.busy.set(true); this.error.set('');
    try {
      this.user.set(await api<User>('/auth/' + this.authMode, 'POST', this.authForm));
      this.authForm.password = ''; this.busy.set(false); this.close();
      this.notice.set('Você entrou na sua conta.'); await this.refresh();
    } catch (e) { this.fail(e); } finally { this.busy.set(false); }
  }
  async logout() {
    this.busy.set(true);
    try { await api('/auth/logout', 'POST'); this.user.set(null); await this.navigate('catalogo'); }
    catch (e) { this.fail(e); } finally { this.busy.set(false); }
  }
  editAnimal(animal?: Animal) {
    this.editingId = animal?.id ?? null;
    this.animalForm = animal ? { name: animal.name, species: animal.species, size: animal.size, ageMonths: animal.ageMonths, city: animal.city, description: animal.description, care: animal.care, photoUrl: animal.photoUrl, vaccinated: animal.vaccinated, neutered: animal.neutered } : this.blankAnimal();
    this.open('animal');
  }
  async saveAnimal() {
    this.busy.set(true); this.error.set('');
    try {
      await api('/animals' + (this.editingId ? '/' + this.editingId : ''), this.editingId ? 'PUT' : 'POST', this.animalForm);
      this.busy.set(false); this.close(); await this.refresh(); this.notice.set('Perfil do animal salvo.');
    } catch (e) { this.fail(e); } finally { this.busy.set(false); }
  }
  startApplication() { this.application = { housing: '', routine: '', experience: '', commitment: false }; this.open('apply'); }
  async submitApplication() {
    this.busy.set(true); this.error.set('');
    try {
      await api('/animals/' + this.selected()!.id + '/requests', 'POST', this.application);
      this.busy.set(false); this.close(); await this.navigate('pedidos'); this.notice.set('Solicitação enviada. O responsável vai analisar suas respostas.');
    } catch (e) { this.fail(e); } finally { this.busy.set(false); }
  }
  prepareDecision(request: AdoptionRequest, status: RequestStatus) {
    this.decisionRequest = request; this.decisionStatus = status; this.decisionNote = ''; this.open('decision');
  }
  async submitDecision() {
    this.busy.set(true); this.error.set('');
    try {
      await api('/requests/' + this.decisionRequest!.id, 'PATCH', { status: this.decisionStatus, note: this.decisionNote });
      this.busy.set(false); this.close(); await this.refresh(); this.notice.set('Solicitação atualizada.');
    } catch (e) { this.fail(e); } finally { this.busy.set(false); }
  }
  age(months: number) {
    if (months === 0) return 'Menos de 1 mês';
    if (months < 12) return months + (months === 1 ? ' mês' : ' meses');
    const years = Math.floor(months / 12); const remainder = months % 12;
    return years + (years === 1 ? ' ano' : ' anos') + (remainder ? ' e ' + remainder + (remainder === 1 ? ' mês' : ' meses') : '');
  }
  imageFailed(event: Event) { (event.target as HTMLImageElement).style.display = 'none'; }
}
