/// <reference types="cypress" />

const existingTodos = [
    'Feed the cat',
    'Do dishes',
];

describe('no todos exist in localstorage', () => {
    beforeEach(() => {
        cy.visit('/')
        cy.get('#new-todo-txt').type(existingTodos[0])
        cy.get('#new-todo-btn').click()
        cy.get('#new-todo-txt').type(existingTodos[1])
        cy.get('#new-todo-btn').click()
        cy.get('#todo-list li').should('have.length', 2)
    })

    it('adds and deletes a todo', () => {
        const todo = 'Pay bills'
        cy.get('#new-todo-txt')
            .type(todo)
        cy.get('#new-todo-btn')
            .click()
        cy.get('#todo-list li')
            .should('have.length', 3)
            .last()
            .should('have.text', todo)
            .should(() => {
                const expectedMeta = '["todo:1" "todo:2" "todo:3"]'
                expect(localStorage.getItem('meta:ids')).to.eq(expectedMeta)
                expect(localStorage.getItem('todo:1')).to.not.be.null
                expect(localStorage.getItem('todo:2')).to.not.be.null
                expect(localStorage.getItem('todo:3')).to.not.be.null
            })

        cy.get('[data-delete=1]')
            .click()
        cy.get('#todo-list li')
            .should('have.length', 2)
            .first()
            .should('have.text', existingTodos[1])
            .should(() => {
                const expectedMeta = '["todo:2" "todo:3"]'
                expect(localStorage.getItem('meta:ids')).to.eq(expectedMeta)
                expect(localStorage.getItem('todo:1')).to.be.null
                expect(localStorage.getItem('todo:2')).to.not.be.null
                expect(localStorage.getItem('todo:3')).to.not.be.null
            })
    })

    it('checks off a todo', () => {
        cy.get('[data-done=1]')
            .click()
        cy.get('#todo-list li')
            .should('have.length', 2)
            .first()
            .should('have.attr', 'data-is-done', "true")
            .should(() => {
                const expectedMeta = '["todo:1" "todo:2"]'
                expect(localStorage.getItem('meta:ids')).to.eq(expectedMeta)
                expect(localStorage.getItem('todo:1')).to.contain(':done true')
                expect(localStorage.getItem('todo:2')).to.contain(':done false')
            })

        cy.get('[data-done=1]')
            .click()
        cy.get('#todo-list li')
            .should('have.length', 2)
            .first()
            .should('have.attr', 'data-is-done', "false")
            .should(() => {
                const expectedMeta = '["todo:1" "todo:2"]'
                expect(localStorage.getItem('meta:ids')).to.eq(expectedMeta)
                expect(localStorage.getItem('todo:1')).to.contain(':done false')
                expect(localStorage.getItem('todo:2')).to.contain(':done false')
            })
    })

    it('edits a todo', () => {
        const oldText = existingTodos[0];
        const newText = 'Feed the dog';

        cy.get('[data-edit=1]')
            .click()
            .should('be.checked')
            .parent()
            .should('not.have.text', oldText)

        cy.get('[data-input=1]')
            .should('have.value', oldText)
            .type('{backspace}{backspace}{backspace}dog')

        cy.get('[data-input=1]')
            .should('have.value', newText)
            .should(() => {
                const expectedMeta = '["todo:1" "todo:2"]'
                expect(localStorage.getItem('meta:ids')).to.eq(expectedMeta)
                expect(localStorage.getItem('todo:1')).to.contain(`:txt "${oldText}"`)
            })

        cy.get('[data-edit=1]')
            .click()
            .should('not.be.checked')
            .parent()
            .should('have.text', newText)
            .should(() => {
                const expectedMeta = '["todo:1" "todo:2"]'
                expect(localStorage.getItem('meta:ids')).to.eq(expectedMeta)
                expect(localStorage.getItem('todo:1')).to.contain(`:txt "${newText}"`)
            })

        cy.get('[data-input=1]')
            .should('not.exist')
    })

    it.skip('edits the middle of a todo text', () => {
        const oldText = existingTodos[0];
        const newText = 'Lastly Feed the cat';

        cy.get('[data-edit=1]')
            .click()

        cy.get('[data-input=1]')
            .should('have.value', oldText)
            .type('{moveToStart}Lastly ')

        cy.get('[data-input=1]')
            .should('have.value', newText)

        cy.get('[data-edit=1]')
            .click()
            .parent()
            .should('have.text', newText)
    })

    it('enters an edited todo on enter keypress', () => {
        const oldText = existingTodos[0];
        const newText = 'Feed the dog';

        cy.get('[data-edit=1]')
            .click()
            .should('be.checked')
            .parent()
            .should('not.have.text', oldText)

        cy.get('[data-input=1]')
            .should('have.value', oldText)
            .type('{backspace}{backspace}{backspace}dog{enter}')

        cy.get('[data-edit=1]')
            .should('not.be.checked')
            .parent()
            .should('have.text', newText)

        cy.get('[data-input=1]')
            .should('not.exist')
    })
})

describe('some todos exist in localstorage', () => {
    beforeEach(() => {
        cy.visit('/')
        localStorage.setItem('meta:ids', '["todo:100" "todo:200"]')
        localStorage.setItem('todo:100', '{:txt "eat" :id 100 :done false}')
        localStorage.setItem('todo:200', '{:txt "sleep" :id 200 :done true}')
    })

    it('loads the localstorage todos', () => {
        cy.get('#todo-list li')
            .should('have.length', 2)

        cy.get('[data-id=100]')
            .should('have.attr', 'data-is-done', 'false')
            .find('.txt')
            .should('have.text', 'eat')
        cy.get('[data-id=200]')
            .should('have.attr', 'data-is-done', 'true')
            .find('.txt')
            .should('have.text', 'sleep')
    })
})

describe('undo and redo', () => {
    beforeEach(() => {
        cy.visit('/')
    })

    it('undo/redo buttons disable property', () => {
        cy.get('#undo-btn').should('be.disabled')
        cy.get('#redo-btn').should('be.disabled')

        cy.get('#new-todo-txt').type('Some new todo')
        cy.get('#new-todo-btn').click()

        cy.get('#undo-btn').should('not.be.disabled')
        cy.get('#redo-btn').should('be.disabled')

        cy.get('#undo-btn').click()
        cy.get('#redo-btn').should('not.be.disabled')
    })

    describe('undo/redo after some action is performed', () => {
        beforeEach(() => {
            cy.get('#new-todo-txt').type(existingTodos[0])
            cy.get('#new-todo-btn').click()
        })

        it('add todo action', () => {
            const todoTxt = 'Second todo item'
            cy.get('#new-todo-txt').type(todoTxt)
            cy.get('#new-todo-btn').click()

            cy.get('#undo-btn').click()

            cy.get('#todo-list li')
                .should('have.length', 1)
                .first()
                .should('have.text', existingTodos[0])
                .should(() => {
                    const expectedMeta = '["todo:1"]'
                    expect(localStorage.getItem('meta:ids')).to.eq(expectedMeta)
                    expect(localStorage.getItem('todo:1')).to.not.be.null
                    expect(localStorage.getItem('todo:2')).to.be.null
                })

            cy.get('#redo-btn').click()

            cy.get('#todo-list li')
                .should('have.length', 2)
                .last()
                .should('have.text', todoTxt)
                .should(() => {
                    const expectedMeta = '["todo:1" "todo:2"]'
                    expect(localStorage.getItem('meta:ids')).to.eq(expectedMeta)
                    expect(localStorage.getItem('todo:1')).to.not.be.null
                    expect(localStorage.getItem('todo:2')).to.not.be.null
                })
        })

        it('delete todo action', () => {
            cy.get('#new-todo-txt').type('two')
            cy.get('#new-todo-btn').click()

            // delete at the top or middle of the list (not last)
            cy.get('[data-delete=1]').click()

            cy.get('#undo-btn').click()

            cy.get('#todo-list li')
                .should('have.length', 2)
                .first()
                .should('have.text', existingTodos[0])
                .should(() => {
                    const expectedMeta = '["todo:2" "todo:1"]'
                    expect(localStorage.getItem('meta:ids')).to.eq(expectedMeta)
                    expect(localStorage.getItem('todo:1')).to.not.be.null
                    expect(localStorage.getItem('todo:2')).to.not.be.null
                })

            cy.get('#redo-btn').click()

            cy.get('#todo-list li')
                .should('have.length', 1)
                .first()
                .should('have.text', 'two')
                .should(() => {
                    const expectedMeta = '["todo:2"]'
                    expect(localStorage.getItem('meta:ids')).to.eq(expectedMeta)
                    expect(localStorage.getItem('todo:1')).to.be.null
                    expect(localStorage.getItem('todo:2')).to.not.be.null
                })
        })

        it('done todo action', () => {
            cy.get('[data-done=1]').click()

            cy.get('#undo-btn').click()

            cy.get('#todo-list li')
                .first()
                .should('have.attr', 'data-is-done', "false")
                .should(() => {
                    expect(localStorage.getItem('todo:1')).to.contain(':done false')
                })

            cy.get('#redo-btn').click()

            cy.get('#todo-list li')
                .first()
                .should('have.attr', 'data-is-done', "true")
                .should(() => {
                    expect(localStorage.getItem('todo:1')).to.contain(':done true')
                })
        })

        it('non-undoable event in between two undoable events', () => {
            const expectThreeTodosInLocal = () => {
                const expectedMeta = '["todo:1" "todo:2" "todo:3"]'
                expect(localStorage.getItem('meta:ids')).to.eq(expectedMeta)
                expect(localStorage.getItem('todo:1')).to.not.be.null
                expect(localStorage.getItem('todo:2')).to.not.be.null
                expect(localStorage.getItem('todo:3')).to.not.be.null
            }
            const expectTwoTodosInLocal = () => {
                const expectedMeta = '["todo:1" "todo:2"]'
                expect(localStorage.getItem('meta:ids')).to.eq(expectedMeta)
                expect(localStorage.getItem('todo:1')).to.not.be.null
                expect(localStorage.getItem('todo:2')).to.not.be.null
                expect(localStorage.getItem('todo:3')).to.be.null
            }
            const expectOneTodoInLocal = () => {
                const expectedMeta = '["todo:1"]'
                expect(localStorage.getItem('meta:ids')).to.eq(expectedMeta)
                expect(localStorage.getItem('todo:1')).to.not.be.null
                expect(localStorage.getItem('todo:2')).to.be.null
            }

            cy.get('#new-todo-txt').type('Second todo item')
            cy.get('#new-todo-btn').click()                  // adding is undoable
            cy.get('#new-todo-txt').type('Third todo item')  // typing a new item is non-undoable
            cy.get('#new-todo-btn').click()                  // adding is undoable

            cy.get('#undo-btn').click()

            cy.get('#todo-list li')
                .should('have.length', 2)
                .first()
                .should('have.text', existingTodos[0])
                .should(expectTwoTodosInLocal)

            cy.get('#undo-btn').click()

            cy.get('#todo-list li')
                .should('have.length', 1)
                .first()
                .should('have.text', existingTodos[0])
                .should(expectOneTodoInLocal)

            cy.get('#redo-btn').click()

            cy.get('#todo-list li')
                .should('have.length', 2)
                .last()
                .should('have.text', 'Second todo item')
                .should(expectTwoTodosInLocal)

            cy.get('#redo-btn').click()

            cy.get('#todo-list li')
                .should('have.length', 3)
                .last()
                .should('have.text', 'Third todo item')
                .should(expectThreeTodosInLocal)
        })
    })
})
