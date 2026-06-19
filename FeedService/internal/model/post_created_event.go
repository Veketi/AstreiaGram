package model

type PostCreatedEvent struct {
	PostID string `json:"postId"`
	AuthorID string `json:"authorId"`
	CreatedAt int64 `json:"createdAt"`
}
