import SwiftUI

struct NotesSectionView: View {
    let notes: [NoteItem]
    let onAddNote: () -> Void
    let onUpdateNote: (String, String, String) -> Void
    let onDeleteNote: (String) -> Void

    var body: some View {
        VStack(spacing: 0) {
            HStack {
                Label("附注", systemImage: "info.circle")
                    .font(.subheadline.weight(.semibold))
                Spacer()
                Button(action: onAddNote) {
                    Label("新增", systemImage: "plus")
                }
                .buttonStyle(.bordered)
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 12)
            .background(.secondary.opacity(0.06))

            if notes.isEmpty {
                Text("这里可以填写不参与汇总的其他信息。")
                    .font(.footnote)
                    .foregroundStyle(.secondary)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(16)
            } else {
                VStack(spacing: 0) {
                    ForEach(notes) { note in
                        NoteRow(
                            note: note,
                            onLabelChange: { onUpdateNote(note.id, $0, note.value) },
                            onValueChange: { onUpdateNote(note.id, note.label, $0) },
                            onDelete: { onDeleteNote(note.id) }
                        )
                        Divider().opacity(0.4)
                    }
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 8)
            }
        }
        .background(.background, in: RoundedRectangle(cornerRadius: 16, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: 16, style: .continuous)
                .strokeBorder(.quaternary, lineWidth: 1)
        )
    }
}

private struct NoteRow: View {
    let note: NoteItem
    let onLabelChange: (String) -> Void
    let onValueChange: (String) -> Void
    let onDelete: () -> Void

    @State private var labelText: String
    @State private var valueText: String

    init(note: NoteItem, onLabelChange: @escaping (String) -> Void, onValueChange: @escaping (String) -> Void, onDelete: @escaping () -> Void) {
        self.note = note
        self.onLabelChange = onLabelChange
        self.onValueChange = onValueChange
        self.onDelete = onDelete
        _labelText = State(initialValue: note.label)
        _valueText = State(initialValue: note.value)
    }

    var body: some View {
        HStack(spacing: 12) {
            TextField("标签…", text: $labelText)
                .textFieldStyle(.roundedBorder)
                .onChange(of: labelText) { newValue in
                    onLabelChange(newValue)
                }

            TextField("值…", text: $valueText)
                .textFieldStyle(.roundedBorder)
                .onChange(of: valueText) { newValue in
                    onValueChange(newValue)
                }

            Button(role: .destructive, action: onDelete) {
                Image(systemName: "trash")
            }
            .buttonStyle(.borderless)
        }
        .onChange(of: note.label) { newValue in
            if newValue != labelText { labelText = newValue }
        }
        .onChange(of: note.value) { newValue in
            if newValue != valueText { valueText = newValue }
        }
    }
}
