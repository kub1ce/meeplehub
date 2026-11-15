package ru.meeplehub.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.meeplehub.domain.entity.BoardGame;
import ru.meeplehub.repository.BoardGameRepository;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/games")
public class BoardGamePageController {

    private final BoardGameRepository boardGameRepository;

    public BoardGamePageController(BoardGameRepository boardGameRepository) {
        this.boardGameRepository = boardGameRepository;
    }

    @GetMapping
    public String showGames(Model model) {
        List<BoardGame> games = new ArrayList<>();
        boardGameRepository.findAll().forEach(games::add);

        model.addAttribute("games", games);

        return "board-games";
    }
}