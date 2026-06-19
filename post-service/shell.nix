{ pkgs ? import <nixpkgs> {} }:

pkgs.mkShell {
  buildInputs = [
    pkgs.jdk25        # troca o jdk que estava aqui
    pkgs.maven
  ];

  JAVA_HOME = "${pkgs.jdk25}";
}
