FROM gitpod/workspace-full:latest

USER root

RUN ["apt-get", "update"]

RUN ["apt-get", "install", "-y", "zsh"]

USER gitpod

ENV ZSH_THEME cloud

RUN sh -c "$(wget -O- https://github.com/deluan/zsh-in-docker/releases/download/v1.1.1/zsh-in-docker.sh)" -- \
    -p git

CMD [ "zsh" ]

RUN brew update
RUN brew install scala sbt
