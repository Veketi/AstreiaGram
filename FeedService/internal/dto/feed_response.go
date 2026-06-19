package dto

type FeedResponse struct {
	UserID string `json:"userId"`
	Posts []string `json:"posts"`
	Page int64 `json:"page"`
	Limit int64 `json:"limit"`
}
