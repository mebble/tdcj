/// <reference types="cypress" />

describe('basics', () => {
    beforeEach(() => {
        cy.visit('/')
    })

    it('adds a new todo', () => {
        const todo = 'Do dishes'
        cy.get('#todo-list li').should('have.length', 0)
        cy.get('#new-todo-txt').type(todo)
        cy.get('#new-todo-btn').click()

        cy.get('#todo-list li').last().should('have.text', todo)
    })
})
