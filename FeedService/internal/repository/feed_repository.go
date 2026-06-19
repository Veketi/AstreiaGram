package repository

import (
	"context"
	"fmt"

	"github.com/redis/go-redis/v9"
)

const maxFeedSize = 1000

type FeedRepository struct {
	client *redis.Client
}

func NewFeedRepository(client *redis.Client) *FeedRepository {
	return &FeedRepository{client: client}
}

func feedKey(userID string) string {
	return fmt.Sprintf("feed:%s", userID)
}

func (r *FeedRepository) AddToFeed(
	ctx context.Context, 
	userID string, 
	postID string,
	timestamp int64,
) error {
	key := feedKey(userID)

	err := r.client.ZAdd(ctx, key, redis.Z{
		Score: float64(timestamp),
		Member: postID,
	}).Err()

	if err != nil {
		return err
	}

	size, err := r.client.ZCard(ctx, key).Result()

	if err != nil {
		return err
	}

	if size > maxFeedSize {
		excess := size - maxFeedSize

		err = r.client.ZRemRangeByRank(ctx, key, 0, excess - 1).Err()

		if err != nil {
			return err
		}
	}

	return nil
}

func (r *FeedRepository) GetFeed(
	ctx context.Context, 
	userID string, 
	offset int64,
	limit int64,
) ([]string, error) {
	return r.client.ZRangeArgs(ctx, redis.ZRangeArgs{
		Key: feedKey(userID),
		Start: offset,
		Stop: offset + limit - 1,
		Rev: true,
	}).Result()
}
