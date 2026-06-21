package util

import "github.com/Veketi/astreiagram/feed-service/internal/dto"

func ReorderPosts(postIDs []string, posts []dto.PostResponse) []dto.PostResponse {
	postMap := make(map[string]dto.PostResponse, len(posts))
	for _, p := range posts {
		postMap[p.ID] = p
	}

	ordered := make([]dto.PostResponse, 0, len(postIDs))
	for _, id := range postIDs {
		if p, ok := postMap[id]; ok {
			ordered = append(ordered, p)
		}
	}
	return ordered
}
