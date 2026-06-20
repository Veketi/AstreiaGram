package dto

type FeedResponse struct {
	UserID string `json:"userId"`
	Posts []PostResponse `json:"posts"`
	Page int64 `json:"page"`
	Limit int64 `json:"limit"`
}
