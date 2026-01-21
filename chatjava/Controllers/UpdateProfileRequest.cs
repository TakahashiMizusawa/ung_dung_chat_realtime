namespace chatjava.Controllers
{
    public class UpdateProfileRequest
    {
        public string? FullName { get; set; }
        public string? OldPassword { get; set; } // Mật khẩu hiện tại
        public string? Password { get; set; }    // Mật khẩu mới
    }
}