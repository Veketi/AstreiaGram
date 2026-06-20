package dto

type UsersReponse struct {
	Users []UserDTO `json:"users"`
	Total int `json:"total"`
}
