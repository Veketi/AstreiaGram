package dto

type PostResponse struct {
	ID           string    `json:"id"`
	UserID       string    `json:"userId"`
	ImageURL     string    `json:"imageUrl"`
	Caption      string    `json:"caption"`
	CreatedAt    string    `json:"createdAt"`
	LikeCount    int       `json:"likeCount"`
	CommentCount int       `json:"commentCount"`
}
