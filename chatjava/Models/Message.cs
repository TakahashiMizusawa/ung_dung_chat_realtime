namespace chatjava.Models
{
    public class Message
    {
        public int Id { get; set; }
        public int SenderId { get; set; }
        public int ReceiverId { get; set; }
        public string Content { get; set; }
        public DateTime Timestamp { get; set; } = DateTime.Now;

        public string? Reaction { get; set; }

        public virtual User Sender { get; set; }
        public virtual User Receiver { get; set; }
    }
}