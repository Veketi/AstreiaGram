package service

import "context"

type FollowerService interface {
	GetFollowers(
		ctx context.Context,
		userID string,
	) ([]string, error)
}
