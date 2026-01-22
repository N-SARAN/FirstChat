import { Component, inject, OnInit, ChangeDetectorRef } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common'; 
import { FormsModule } from '@angular/forms'; 
import { environment } from '../environments/environment';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.html',
  styleUrls: ['./app.css']
})
export class App implements OnInit {
  http = inject(HttpClient);
  cdr = inject(ChangeDetectorRef);

  // STATE
  currentUser: any = null;    
  selectedUser: any = null;   
  
  // DATA
  users: any[] = [];
  messages: any[] = [];
  
  // INPUTS
  loginData = { username: '', password: '' };
  newMessage: string = "";

  ngOnInit() {
    const saved = localStorage.getItem('user');
    if (saved) {
      this.currentUser = JSON.parse(saved);
      this.loadData();
    }
  }

  // --- AUTH ACTIONS ---
  login() {
    // FIX: Uses Render URL + /api/login
    this.http.post(`${environment.apiUrl}/api/login`, this.loginData)
      .subscribe({
        next: (user: any) => {
          this.currentUser = user;
          localStorage.setItem('user', JSON.stringify(user)); 
          this.loadData();
        },
        error: () => alert('Login Failed! Check Password or Connection.')
      });
  }

  logout() {
    this.currentUser = null;
    this.selectedUser = null;
    localStorage.removeItem('user');
  }

  // --- CHAT ACTIONS ---
  loadData() {
    this.fetchUsers();
    this.fetchMessages();
    // Poll every 2 seconds
    setInterval(() => this.fetchMessages(), 2000);
  }

  fetchUsers() {
    this.http.get<any[]>(`${environment.apiUrl}/api/users`)
      .subscribe(data => {
        if(this.currentUser) {
            this.users = data.filter(u => u.username !== this.currentUser.username);
        }
      });
  }

  fetchMessages() {
    let url = `${environment.apiUrl}/api/messages`;
    
    if (this.selectedUser) {
      url += `?user1=${this.currentUser.username}&user2=${this.selectedUser.username}`;
    }

    this.http.get<any[]>(url).subscribe(data => {
        this.messages = data;
        this.cdr.detectChanges();
    });
  }

  selectChat(user: any) {
    this.selectedUser = user; 
    this.fetchMessages();
  }

  sendMessage() {
    if (!this.newMessage.trim()) return;

    const msg = {
      text: this.newMessage,
      sender: this.currentUser.username,
      receiver: this.selectedUser ? this.selectedUser.username : null 
    };

    this.http.post(`${environment.apiUrl}/api/messages`, msg)
      .subscribe(() => {
        this.newMessage = "";
        this.fetchMessages();
      });
  }
}