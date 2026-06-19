package service

import "context"

type MockFollowerService struct {}

func NewMockFollowerService() *MockFollowerService {
	return &MockFollowerService{}
}

func (s *MockFollowerService) GetFollowers(
	ctx context.Context,
	userId string,
) ([]string, error) {
	return []string {
		"2",
		"3",
		"4",
	}, nil
}
