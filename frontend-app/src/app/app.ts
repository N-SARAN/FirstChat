import { Component, inject, OnInit, ChangeDetectorRef } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common'; 
import { FormsModule } from '@angular/forms'; 

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
  currentUser: any = null;    // Who am I?
  selectedUser: any = null;   // Who am I talking to? (null = Group Chat)
  
  // DATA
  users: any[] = [];
  messages: any[] = [];
  
  // INPUTS
  loginData = { username: '', password: '' };
  newMessage: string = "";

  ngOnInit() {
    // Check if already logged in
    const saved = localStorage.getItem('user');
    if (saved) {
      this.currentUser = JSON.parse(saved);
      this.loadData();
    }
  }

  // --- AUTH ACTIONS ---
  login() {
    this.http.post('http://localhost:8080/api/login', this.loginData)
      .subscribe({
        next: (user: any) => {
          this.currentUser = user;
          localStorage.setItem('user', JSON.stringify(user)); // Save session
          this.loadData();
        },
        error: () => alert('Login Failed!')
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
    // Auto-refresh every 2 seconds (Polling)
    setInterval(() => this.fetchMessages(), 2000);
  }

  fetchUsers() {
    this.http.get<any[]>('http://localhost:8080/api/users')
      .subscribe(data => {
        // Remove myself from list
        this.users = data.filter(u => u.username !== this.currentUser.username);
      });
  }

  fetchMessages() {
    let url = 'http://localhost:8080/api/messages';
    
    // If Private Chat: Add ?user1=Me&user2=Them
    if (this.selectedUser) {
      url += `?user1=${this.currentUser.username}&user2=${this.selectedUser.username}`;
    }

    this.http.get<any[]>(url).subscribe(data => {
        this.messages = data;
        this.cdr.detectChanges();
    });
  }

  selectChat(user: any) {
    this.selectedUser = user; // null means Group Chat
    this.fetchMessages();
  }

  sendMessage() {
    if (!this.newMessage.trim()) return;

    const msg = {
      text: this.newMessage,
      sender: this.currentUser.username,
      receiver: this.selectedUser ? this.selectedUser.username : null // null = Group
    };

    this.http.post('http://localhost:8080/api/messages', msg)
      .subscribe(() => {
        this.newMessage = "";
        this.fetchMessages();
      });
  }
}