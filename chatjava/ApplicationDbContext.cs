using Microsoft.EntityFrameworkCore;
using System.ComponentModel.DataAnnotations.Schema;

namespace chatjava
{
    public class ApplicationDbContext : DbContext
    {
        public ApplicationDbContext(DbContextOptions<ApplicationDbContext> options)
            : base(options) { }

        public DbSet<User> Users { get; set; }
        public DbSet<Friendship> Friendships { get; set; }
        public DbSet<Message> Messages { get; set; }

        // Alias để sửa lỗi trong FriendController mà không gây lỗi trùng lặp DbSet
        public IQueryable<Friendship> FriendRequests => Friendships;

        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            base.OnModelCreating(modelBuilder);

            // Cấu hình quan hệ cho bảng Bạn bè
            modelBuilder.Entity<Friendship>(entity => {
                entity.ToTable("Friendships"); // Đảm bảo chỉ tạo 1 bảng duy nhất
                entity.HasOne(f => f.Sender).WithMany().HasForeignKey(f => f.SenderId).OnDelete(DeleteBehavior.Restrict);
                entity.HasOne(f => f.Receiver).WithMany().HasForeignKey(f => f.ReceiverId).OnDelete(DeleteBehavior.Restrict);
            });

            // Cấu hình quan hệ cho bảng Tin nhắn
            modelBuilder.Entity<Message>(entity => {
                entity.ToTable("Messages");
                entity.HasOne(m => m.Sender).WithMany().HasForeignKey(m => m.SenderId).OnDelete(DeleteBehavior.Restrict);
                entity.HasOne(m => m.Receiver).WithMany().HasForeignKey(m => m.ReceiverId).OnDelete(DeleteBehavior.Restrict);
            });
        }
    }

    public class User
    {
        public int Id { get; set; }
        public string Username { get; set; } = string.Empty;
        public string Password { get; set; } = string.Empty;
        public string FullName { get; set; } = string.Empty;
        public string? Avatar { get; set; }

        // --- BỔ SUNG CÁC TRƯỜNG BẢO MẬT MỚI ---
        public string? SecurityQuestion { get; set; } // Câu hỏi bảo mật
        public string? SecurityAnswer { get; set; }   // Câu trả lời bảo mật
        public string? PinCode { get; set; }          // Mã PIN bảo mật
    }

    public class Message
    {
        public int Id { get; set; }
        public int SenderId { get; set; }
        public int ReceiverId { get; set; }
        public string Content { get; set; } = string.Empty;
        public DateTime Timestamp { get; set; } = DateTime.Now;
        public string Reaction { get; set; } = string.Empty;

        [ForeignKey("SenderId")] public virtual User? Sender { get; set; }
        [ForeignKey("ReceiverId")] public virtual User? Receiver { get; set; }
    }

    public class Friendship
    {
        public int Id { get; set; }
        public int SenderId { get; set; }
        public int ReceiverId { get; set; }
        public int Status { get; set; } // 0: Pending, 1: Accepted
        public DateTime CreatedAt { get; set; } = DateTime.Now;

        [ForeignKey("SenderId")] public virtual User? Sender { get; set; }
        [ForeignKey("ReceiverId")] public virtual User? Receiver { get; set; }
    }

    public class FriendRequest : Friendship { }
}