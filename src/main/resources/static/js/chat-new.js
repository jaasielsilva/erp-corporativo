document.addEventListener('DOMContentLoaded',function(){
const roomsList=document.getElementById('roomsList');
const messagesArea=document.getElementById('messagesArea');
const roomHeader=document.getElementById('roomHeader');
const messageInput=document.getElementById('messageInput');
const sendBtn=document.getElementById('sendBtn');
const attachBtn=document.getElementById('attachBtn');
const attachInput=document.getElementById('attachInput');
const roomSearch=document.getElementById('roomSearch');
let stomp=null;let currentRoomId=null;let roomSub=null;const ctx=window.CHAT_CONTEXT||{};
function connect(){stomp=new StompJs.Client({webSocketFactory:()=>new SockJS('/ws'),reconnectDelay:5000,heartbeatIncoming:10000,heartbeatOutgoing:10000,onConnect:onConnected});stomp.activate();}
function onConnected(){loadRooms();}
function loadRooms(){fetch('/api/chat/rooms').then(r=>r.json()).then(data=>{roomsList.innerHTML='';(data||[]).forEach(addRoomItem);if(data&&data.length>0)selectRoom(data[0].id);});}
function addRoomItem(room){const el=document.createElement('button');el.type='button';el.className='list-group-item list-group-item-action';el.textContent=room.name;el.dataset.roomId=room.id;el.addEventListener('click',()=>selectRoom(room.id));roomsList.appendChild(el);} 
function selectRoom(id){currentRoomId=id;if(roomSub)roomSub.unsubscribe();roomSub=stomp.subscribe(`/topic/chat.room.${id}`,onRoomEvent);roomHeader.textContent='Sala #'+id;messagesArea.innerHTML='';fetch(`/api/chat/rooms/${id}/messages`).then(r=>r.json()).then(msgs=>{(msgs||[]).forEach(displayMessage);messagesArea.scrollTop=messagesArea.scrollHeight;});}
function onRoomEvent(frame){try{const msg=JSON.parse(frame.body);if(msg.roomId===currentRoomId)displayMessage(msg);}catch(e){}}
function displayMessage(m){const mine=(m.senderId===ctx.usuarioId);const wrap=document.createElement('div');wrap.className='d-flex mb-2';wrap.style.justifyContent=mine?'flex-end':'flex-start';const bubble=document.createElement('div');bubble.className='p-2 rounded';bubble.style.maxWidth='70%';bubble.style.backgroundColor=mine?'#0d6efd':'#f1f3f5';bubble.style.color=mine?'#fff':'#000';const header=document.createElement('div');header.className='small fw-semibold';header.textContent=m.senderName||'';const body=document.createElement('div');if(m.type==='IMAGE'&&m.fileUrl){const img=document.createElement('img');img.src='/' + m.fileUrl;img.style.maxWidth='300px';img.style.borderRadius='8px';body.appendChild(img);if(m.content){const p=document.createElement('div');p.textContent=m.content;body.appendChild(p);} } else if(m.type==='FILE'&&m.fileUrl){const a=document.createElement('a');a.href='/' + m.fileUrl;a.target='_blank';a.textContent=m.fileName||'Arquivo';body.appendChild(a);if(m.content){const p=document.createElement('div');p.textContent=m.content;body.appendChild(p);} } else {body.textContent=m.content;} const time=document.createElement('div');time.className='text-muted small';time.textContent=(m.sentAt||'').toString().slice(11,16);bubble.appendChild(header);bubble.appendChild(body);bubble.appendChild(time);wrap.appendChild(bubble);messagesArea.appendChild(wrap);messagesArea.scrollTop=messagesArea.scrollHeight;}
sendBtn.addEventListener('click',sendText);messageInput.addEventListener('keypress',function(e){if(e.key==='Enter'){e.preventDefault();sendText();}});
function sendText(){const content=(messageInput.value||'').trim();if(!content||!currentRoomId)return;fetch(`/api/chat/rooms/${currentRoomId}/messages`,{method:'POST',headers:{'Content-Type':'application/x-www-form-urlencoded'},body:new URLSearchParams({content})}).then(r=>r.json()).then(()=>{messageInput.value='';});}
attachBtn.addEventListener('click',()=>attachInput.click());attachInput.addEventListener('change',function(){const f=this.files&&this.files[0];if(!f||!currentRoomId)return;const fd=new FormData();fd.append('file',f);fd.append('content',f.name);fetch(`/api/chat/rooms/${currentRoomId}/messages/upload`,{method:'POST',body:fd}).then(()=>{});this.value='';});
if(roomSearch){roomSearch.addEventListener('input',function(){const term=(this.value||'').toLowerCase();roomsList.querySelectorAll('.list-group-item').forEach(i=>{i.style.display=(i.textContent||'').toLowerCase().includes(term)?'':'none';});});}
connect();
});