namespace chatjava.Models
{
    public class Friendship
    {
        public int Id { get; set; }
        public int SenderId { get; set; }
        public int ReceiverId { get; set; }

        // 0: Đang chờ, 1: Đã đồng ý, 2: Từ chối/Chặn
        public int Status { get; set; }

        // Các thuộc tính điều hướng (Navigation Properties)
        public virtual User Sender { get; set; }
        public virtual User Receiver { get; set; }
    }
}