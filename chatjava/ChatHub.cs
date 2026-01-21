using chatjava;
using Microsoft.AspNetCore.SignalR;
using Microsoft.EntityFrameworkCore;
using System;
using System.Threading.Tasks;

namespace chatjava
{
    public class ChatHub : Hub
    {
        private readonly ApplicationDbContext _context;

        public ChatHub(ApplicationDbContext context)
        {
            _context = context;
        }

        // --- TỰ ĐỘNG JOIN GROUP KHI KẾT NỐI ---
        public override async Task OnConnectedAsync()
        {
            var httpContext = Context.GetHttpContext();
            var userId = httpContext?.Request.Query["userId"];

            if (!string.IsNullOrEmpty(userId))
            {
                // Thêm kết nối vào group theo UserId
                await Groups.AddToGroupAsync(Context.ConnectionId, userId);
                Console.WriteLine($"[SIGNALR] User {userId} đã kết nối (ConnectionId: {Context.ConnectionId})");
            }
            await base.OnConnectedAsync();
        }

        // --- GỬI TIN NHẮN (Text & Media URL) ---
        public async Task SendMessage(int senderId, int receiverId, string message)
        {
            if (string.IsNullOrWhiteSpace(message)) return;

            try
            {
                var newMessage = new Message
                {
                    SenderId = senderId,
                    ReceiverId = receiverId,
                    Content = message,
                    Timestamp = DateTime.Now,
                    Reaction = string.Empty
                };

                _context.Messages.Add(newMessage);
                await _context.SaveChangesAsync();

                // Gửi realtime cho người nhận
                await Clients.Group(receiverId.ToString()).SendAsync("ReceiveMessage", senderId, message, newMessage.Id);

                // Gửi xác nhận cho chính người gửi (để cập nhật ID tin nhắn dưới Android)
                await Clients.Caller.SendAsync("ReceiveMessage", senderId, message, newMessage.Id);
            }
            catch (Exception ex)
            {
                Console.WriteLine($"[ERROR] SendMessage: {ex.Message}");
            }
        }

        // --- THẢ CẢM XÚC ---
        public async Task SendReaction(int receiverId, int messageId, string reaction)
        {
            try
            {
                var msg = await _context.Messages.FindAsync(messageId);
                if (msg != null)
                {
                    msg.Reaction = reaction;
                    await _context.SaveChangesAsync();

                    // Cập nhật emoji cho cả 2 bên
                    await Clients.Group(receiverId.ToString()).SendAsync("ReceiveReaction", messageId, reaction);
                    await Clients.Caller.SendAsync("ReceiveReaction", messageId, reaction);
                }
            }
            catch (Exception ex)
            {
                Console.WriteLine($"[ERROR] SendReaction: {ex.Message}");
            }
        }

        // --- THÔNG BÁO KẾT BẠN ---
        public async Task SendFriendRequest(int receiverId, string senderName)
        {
            try
            {
                await Clients.Group(receiverId.ToString()).SendAsync("ReceiveFriendRequest", senderName);
            }
            catch (Exception ex)
            {
                Console.WriteLine($"[ERROR] SendFriendRequest: {ex.Message}");
            }
        }

        // --- NGẮT KẾT NỐI ---
        public override async Task OnDisconnectedAsync(Exception? exception)
        {
            Console.WriteLine($"[SIGNALR] Connection {Context.ConnectionId} đã ngắt kết nối.");
            await base.OnDisconnectedAsync(exception);
        }

        // Giữ lại để tránh lỗi nếu Android vẫn gọi phương thức cũ
        public async Task JoinGroup(int userId)
        {
            await Groups.AddToGroupAsync(Context.ConnectionId, userId.ToString());
        }
    }
}