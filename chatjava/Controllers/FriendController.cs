using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Microsoft.AspNetCore.SignalR; // Thêm thư viện này
using chatjava;

namespace chatjava.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class FriendController : ControllerBase
    {
        private readonly ApplicationDbContext _context;
        private readonly IHubContext<ChatHub> _hubContext; // Inject ChatHub

        public FriendController(ApplicationDbContext context, IHubContext<ChatHub> hubContext)
        {
            _context = context;
            _hubContext = hubContext;
        }

        // 1. Tìm kiếm người dùng để kết bạn
        [HttpGet("search")]
        public async Task<IActionResult> SearchUser([FromQuery] string username)
        {
            var users = await _context.Users
                .Where(u => u.Username.Contains(username) || u.FullName.Contains(username))
                .Select(u => new { id = u.Id, fullName = u.FullName, avatar = u.Avatar })
                .ToListAsync();
            return Ok(users);
        }

        // 2. Gửi lời mời kết bạn (Đã thêm Real-time)
        [HttpPost("sendRequest")]
        public async Task<IActionResult> SendRequest([FromQuery] int senderId, [FromQuery] int receiverId)
        {
            var exists = await _context.Friendships.AnyAsync(f =>
                (f.SenderId == senderId && f.ReceiverId == receiverId) ||
                (f.SenderId == receiverId && f.ReceiverId == senderId));

            if (exists) return BadRequest(new { message = "Đã gửi lời mời hoặc đã là bạn" });

            _context.Friendships.Add(new Friendship
            {
                SenderId = senderId,
                ReceiverId = receiverId,
                Status = 0
            });
            await _context.SaveChangesAsync();

            // GỬI THÔNG BÁO REAL-TIME ĐẾN NGƯỜI NHẬN
            await _hubContext.Clients.User(receiverId.ToString()).SendAsync("ReceiveFriendRequest", senderId);

            return Ok(new { message = "Đã gửi lời mời" });
        }

        // 3. Lấy danh sách lời mời đang chờ (Status = 0)
        [HttpGet("requests/{userId}")]
        public async Task<IActionResult> GetPendingRequests(int userId)
        {
            var requests = await _context.Friendships
                .Where(f => f.ReceiverId == userId && f.Status == 0)
                .Select(f => new {
                    id = f.Sender.Id,
                    fullName = f.Sender.FullName,
                    avatar = f.Sender.Avatar,
                    lastMessage = "Muốn kết bạn với bạn"
                }).ToListAsync();
            return Ok(requests);
        }

        // 4. Lấy danh sách bạn bè chính thức (Status = 1)
        [HttpGet("list/{userId}")]
        public async Task<IActionResult> GetFriendList(int userId)
        {
            var friends = await _context.Friendships
                .Where(f => (f.SenderId == userId || f.ReceiverId == userId) && f.Status == 1)
                .Select(f => f.SenderId == userId ? f.Receiver : f.Sender)
                .Select(u => new {
                    id = u.Id,
                    fullName = u.FullName,
                    avatar = u.Avatar,
                    lastMessage = "Nhấn để nhắn tin..."
                }).ToListAsync();
            return Ok(friends);
        }

        // 5. Đồng ý hoặc Từ chối lời mời (Đã thêm Real-time)
        [HttpPost("respondRequest")]
        public async Task<IActionResult> RespondRequest([FromQuery] int senderId, [FromQuery] int receiverId, [FromQuery] int status)
        {
            var friendship = await _context.Friendships
                .FirstOrDefaultAsync(f => f.SenderId == senderId && f.ReceiverId == receiverId && f.Status == 0);

            if (friendship == null) return NotFound();

            if (status == 1)
            {
                friendship.Status = 1; // Đồng ý
                // BÁO CHO NGƯỜI GỬI RẰNG LỜI MỜI ĐÃ ĐƯỢC CHẤP NHẬN
                await _hubContext.Clients.User(senderId.ToString()).SendAsync("FriendRequestAccepted", receiverId);
            }
            else
            {
                _context.Friendships.Remove(friendship); // Từ chối
            }

            await _context.SaveChangesAsync();
            return Ok();
        }

        // 6. XÓA BẠN BÈ
        [HttpDelete("deleteFriend")]
        public async Task<IActionResult> DeleteFriend([FromQuery] int userId, [FromQuery] int friendId)
        {
            var friendship = await _context.Friendships
                .FirstOrDefaultAsync(f =>
                    ((f.SenderId == userId && f.ReceiverId == friendId) ||
                     (f.SenderId == friendId && f.ReceiverId == userId))
                    && f.Status == 1);

            if (friendship == null) return NotFound(new { message = "Không tìm thấy quan hệ bạn bè" });

            _context.Friendships.Remove(friendship);
            await _context.SaveChangesAsync();
            return Ok(new { message = "Xóa thành công" });
        }
    }
}