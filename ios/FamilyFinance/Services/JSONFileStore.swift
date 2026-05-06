import Foundation

actor JSONFileStore {
    private let fileURL: URL
    private let encoder: JSONEncoder
    private let decoder: JSONDecoder

    init(fileURL: URL) {
        self.fileURL = fileURL

        let encoder = JSONEncoder()
        encoder.outputFormatting = [.withoutEscapingSlashes]
        self.encoder = encoder

        let decoder = JSONDecoder()
        self.decoder = decoder
    }

    func load<T: Codable>(default defaultValue: T) -> T {
        do {
            guard FileManager.default.fileExists(atPath: fileURL.path) else {
                return defaultValue
            }
            let data = try Data(contentsOf: fileURL)
            return try decoder.decode(T.self, from: data)
        } catch {
            return defaultValue
        }
    }

    func save<T: Codable>(_ value: T) throws {
        let data = try encoder.encode(value)
        try data.write(to: fileURL, options: [.atomic])
    }

    func deleteFile() throws {
        guard FileManager.default.fileExists(atPath: fileURL.path) else { return }
        try FileManager.default.removeItem(at: fileURL)
    }
}

