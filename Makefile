all: rapport.md
	pandoc $^ -o rapport.pdf
