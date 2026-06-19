{
	description = "Feed Service dev shell";

	inputs = {
		nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
		flake-utils.url = "github:numtide/flake-utils";
	};

	outputs = { self, nixpkgs, flake-utils }: 
	flake-utils.lib.eachDefaultSystem (system:
		let
			pkgs = nixpkgs.legacyPackages.${system};
		in {
			devShells.default = pkgs.mkShell {
				buildInputs = with pkgs; [
					go
					gopls
					golangci-lint
					kafkactl
					redis
					docker-compose
				];

				shellHook = ''
					zsh
					echo "Feed Service dev env"
					echo "Go: $(go version)"
				'';
			};
		});
}
