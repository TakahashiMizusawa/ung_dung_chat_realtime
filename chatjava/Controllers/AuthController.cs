using chatjava;
using Microsoft.EntityFrameworkCore;
using Microsoft.AspNetCore.Mvc;

[Route("api/[controller]")]
[ApiController]
public class AuthController : ControllerBase
{
    private readonly ApplicationDbContext _context;
    public AuthController(ApplicationDbContext context) => _context = context;

    [HttpPost("register")]
    public async Task<IActionResult> Register([FromBody] User user)
    {
        if (await _context.Users.AnyAsync(u => u.Username == user.Username))
            return BadRequest(new { message = "Tài khoản đã tồn tại" });

        _context.Users.Add(user);
        await _context.SaveChangesAsync();
        return Ok(new { message = "Đăng ký thành công" });
    }

    [HttpPost("login")]
    public async Task<IActionResult> Login([FromBody] LoginDTO loginInfo)
    {
        var user = await _context.Users.FirstOrDefaultAsync(u =>
            u.Username == loginInfo.Username && u.Password == loginInfo.Password);

        if (user == null) return Unauthorized(new { message = "Sai tài khoản hoặc mật khẩu" });

        // Trả về toàn bộ user (bao gồm cả PinCode) để Android lưu vào SharedPreferences
        return Ok(user);
    }

    // --- MỚI: Lấy câu hỏi bảo mật ---
    [HttpGet("get-security-question/{username}")]
    public async Task<IActionResult> GetSecurityQuestion(string username)
    {
        var user = await _context.Users.FirstOrDefaultAsync(u => u.Username == username);
        if (user == null) return NotFound(new { message = "Không tìm thấy người dùng" });

        return Ok(new { question = user.SecurityQuestion });
    }

    // --- MỚI: Xác nhận trả lời và đặt lại mật khẩu ---
    [HttpPost("reset-password")]
    public async Task<IActionResult> ResetPassword([FromBody] ResetPasswordDTO request)
    {
        var user = await _context.Users.FirstOrDefaultAsync(u => u.Username == request.Username);

        if (user == null) return NotFound(new { message = "Người dùng không tồn tại" });

        // Kiểm tra câu trả lời (Lưu ý: nên dùng .ToLower() hoặc .Trim() để tránh lỗi nhập thừa khoảng trắng)
        if (user.SecurityAnswer?.Trim().ToLower() != request.Answer.Trim().ToLower())
        {
            return BadRequest(new { message = "Câu trả lời bảo mật sai" });
        }

        user.Password = request.NewPassword;
        await _context.SaveChangesAsync();
        return Ok(new { message = "Đổi mật khẩu thành công" });
    }

    [HttpPut("update-avatar/{id}")]
    public async Task<IActionResult> UpdateAvatar(int id, IFormFile file)
    {
        var user = await _context.Users.FindAsync(id);
        if (user == null || file == null) return BadRequest();

        var fileName = Guid.NewGuid().ToString() + Path.GetExtension(file.FileName);
        var filePath = Path.Combine(Directory.GetCurrentDirectory(), "wwwroot/uploads", fileName);

        if (!Directory.Exists(Path.Combine(Directory.GetCurrentDirectory(), "wwwroot/uploads")))
            Directory.CreateDirectory(Path.Combine(Directory.GetCurrentDirectory(), "wwwroot/uploads"));

        using (var stream = new FileStream(filePath, FileMode.Create)) await file.CopyToAsync(stream);

        user.Avatar = "uploads/" + fileName;
        await _context.SaveChangesAsync();
        return Ok(new { avatarUrl = user.Avatar });
    }

    [HttpPut("update-profile/{id}")]
    public async Task<IActionResult> UpdateProfile(int id, [FromBody] ProfileUpdateDTO updateInfo)
    {
        var user = await _context.Users.FindAsync(id);
        if (user == null || user.Password != updateInfo.OldPassword) return BadRequest();

        user.FullName = updateInfo.FullName ?? user.FullName;
        user.Password = updateInfo.Password ?? user.Password;
        await _context.SaveChangesAsync();
        return Ok();
    }

    [HttpDelete("delete-account/{id}")]
    public async Task<IActionResult> DeleteAccount(int id, [FromQuery] string password)
    {
        var user = await _context.Users.FindAsync(id);
        if (user == null || user.Password != password) return BadRequest();

        _context.Users.Remove(user);
        await _context.SaveChangesAsync();
        return Ok();
    }

    // --- DTO CLASSES ---
    public class LoginDTO { public string Username { get; set; } public string Password { get; set; } }

    public class ProfileUpdateDTO { public string? FullName { get; set; } public string? Password { get; set; } public string OldPassword { get; set; } }

    public class ResetPasswordDTO
    {
        public string Username { get; set; }
        public string Answer { get; set; }
        public string NewPassword { get; set; }
    }
}